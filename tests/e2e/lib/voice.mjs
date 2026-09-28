import { execFile } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { promisify } from 'node:util';

const run = promisify(execFile);
const VOICE = 'Microsoft Maria Desktop';
const SILENCE_BEFORE_SPEAKING_MILLIS = 1_500;
const SILENCE_AFTER_SPEAKING_MILLIS = 1_000;

export async function spokenWav(name, sentence) {
  const directory = join(tmpdir(), 'fiesta-e2e-voice');
  mkdirSync(directory, { recursive: true });
  const path = join(directory, `${name}.wav`);
  if (existsSync(path)) return path;
  const script = [
    'Add-Type -AssemblyName System.Speech',
    '$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer',
    `$synth.SelectVoice('${VOICE}')`,
    '$synth.Rate = -2',
    '$format = New-Object System.Speech.AudioFormat.SpeechAudioFormatInfo(16000, [System.Speech.AudioFormat.AudioBitsPerSample]::Sixteen, [System.Speech.AudioFormat.AudioChannel]::Mono)',
    `$synth.SetOutputToWaveFile('${path}', $format)`,
    `$synth.Speak('${sentence.replaceAll("'", "''")}')`,
    '$synth.Dispose()'
  ].join('; ');
  await run('powershell', ['-NoProfile', '-Command', script]);
  writeFileSync(path, canonicalPcmWav(readFileSync(path)));
  return path;
}

function canonicalPcmWav(wav) {
  const dataAt = wav.indexOf('data', 12);
  const bytesPerMillis = wav.readUInt32LE(28) / 1000;
  const silence = (millis) => Buffer.alloc(Math.round(millis * bytesPerMillis / 2) * 2);
  const speech = wav.subarray(dataAt + 8, dataAt + 8 + wav.readUInt32LE(dataAt + 4));
  const data = Buffer.concat([silence(SILENCE_BEFORE_SPEAKING_MILLIS), speech, silence(SILENCE_AFTER_SPEAKING_MILLIS)]);
  const header = Buffer.alloc(44);
  header.write('RIFF', 0);
  header.writeUInt32LE(36 + data.length, 4);
  header.write('WAVEfmt ', 8);
  header.writeUInt32LE(16, 16);
  wav.copy(header, 20, 20, 36);
  header.write('data', 36);
  header.writeUInt32LE(data.length, 40);
  return Buffer.concat([header, data]);
}

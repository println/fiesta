import { execFile, spawn } from 'node:child_process';
import { promisify } from 'node:util';
import { config } from './config.mjs';

const run = promisify(execFile);

export async function adb(...args) {
  const { stdout } = await run(config.adb, ['-s', config.serial, ...args], { maxBuffer: 64 * 1024 * 1024 });
  return stdout;
}

export const shell = (command) => adb('shell', command);

export async function pidOf(packageName = config.packageName) {
  const out = await shell(`pidof ${packageName}`).catch(() => '');
  return out.trim().split(/\s+/)[0] || null;
}

export async function readPrefs(file) {
  return shell(`run-as ${config.packageName} cat shared_prefs/${file}.xml`);
}

export function prefValue(xml, name) {
  const match = xml.match(new RegExp(`name="${name}"(?: value="([^"]*)"\\s*/>|>([^<]*)<)`));
  return match ? (match[1] ?? match[2]) : null;
}

export function unescapeXml(text) {
  return text.replaceAll('&quot;', '"').replaceAll('&apos;', "'").replaceAll('&lt;', '<').replaceAll('&gt;', '>').replaceAll('&amp;', '&');
}

export class Logcat {
  constructor(tags) {
    this.lines = [];
    this.waiters = [];
    const filters = tags.length ? [...tags.map((tag) => `${tag}:V`), '*:S'] : [];
    this.process = spawn(config.adb, ['-s', config.serial, 'logcat', '-v', 'threadtime', '-T', '1', ...filters]);
    let pending = '';
    this.process.stdout.on('data', (chunk) => {
      pending += chunk;
      const complete = pending.split(/\r?\n/);
      pending = complete.pop();
      complete.forEach((line) => this.push(line));
    });
  }

  push(line) {
    this.lines.push(line);
    this.waiters = this.waiters.filter((waiter) => !waiter.check(line));
  }

  mark() {
    return this.lines.length;
  }

  since(mark) {
    return this.lines.slice(mark);
  }

  waitFor(pattern, { from = 0, timeout = 30_000 } = {}) {
    const found = this.lines.slice(from).find((line) => pattern.test(line));
    if (found) return Promise.resolve(found);
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        this.waiters = this.waiters.filter((waiter) => waiter !== entry);
        reject(new Error(`timed out after ${timeout} ms waiting for ${pattern}`));
      }, timeout);
      const entry = {
        check: (line) => {
          if (!pattern.test(line)) return false;
          clearTimeout(timer);
          resolve(line);
          return true;
        }
      };
      this.waiters.push(entry);
    });
  }

  stop() {
    this.process.kill();
  }
}

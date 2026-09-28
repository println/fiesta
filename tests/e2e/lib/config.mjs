const sdk = process.env.ANDROID_SDK ?? process.env.ANDROID_HOME ?? process.env.ANDROID_SDK_ROOT;
if (!sdk) throw new Error('Set ANDROID_SDK (or ANDROID_HOME) to the Android SDK directory');

export const config = {
  adb: process.env.ADB ?? `${sdk}/platform-tools/adb`,
  serial: process.env.ANDROID_SERIAL ?? 'emulator-5554',
  packageName: 'stream.qorvanel.media',
  dhu: `${sdk}/extras/google/auto/desktop-head-unit.exe`,
  dhuConfig: process.env.DHU_CONFIG ?? `${sdk}/extras/google/auto/config/default.ini`,
  videoUrl: process.env.E2E_VIDEO_URL ?? 'https://m.youtube.com/watch?v=tvPSPkugS7A&list=RDr36UCU3ndIM',
  headUnitPort: 5277,
  devtoolsPort: 9333
};

import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiUrl = process.env.VITE_API_BASE_URL || env.VITE_API_BASE_URL;

  if (command === 'build') {
    if (!apiUrl || !apiUrl.trim()) {
      throw new Error(
        '\n[Vite Production Build Error] VITE_API_BASE_URL is missing!\n' +
        'Production builds for Vercel require VITE_API_BASE_URL to point to your deployed backend (e.g., https://your-backend.onrender.com).\n' +
        'Please define VITE_API_BASE_URL in your Vercel Project Settings or environment file.\n'
      );
    }

    try {
      const parsed = new URL(apiUrl.trim());
      if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
        throw new Error(`Protocol "${parsed.protocol}" is invalid. Must be http: or https:.`);
      }
      if (!parsed.hostname) {
        throw new Error('URL must contain a valid hostname.');
      }
    } catch (err) {
      throw new Error(
        `\n[Vite Production Build Error] Invalid VITE_API_BASE_URL ("${apiUrl}"): ${err.message}\n`
      );
    }
  }

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        }
      }
    }
  };
});

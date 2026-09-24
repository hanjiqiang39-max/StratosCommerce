/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        brand: '#38BDF8',
        ink: '#111827',
        mute: '#6B7280',
        page: '#F7F8FA',
      },
      fontFamily: {
        sans: ['Microsoft YaHei UI', 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', 'sans-serif'],
      },
    },
  },
  plugins: [],
}

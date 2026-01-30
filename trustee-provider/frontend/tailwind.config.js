/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#3182F6',
        background: '#F2F4F6',
        surface: '#FFFFFF',
        text: {
          DEFAULT: '#191F28',
          gray: '#8B95A1',
        },
      },
      fontFamily: {
        sans: ['Pretendard', 'sans-serif'],
      },
      maxWidth: {
        'mobile': '450px',
      }
    },
  },
  plugins: [],
}
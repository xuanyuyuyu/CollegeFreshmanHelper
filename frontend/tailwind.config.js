export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#8b261e',
          dark: '#6f1d18',
          pale: '#f7f2f1'
        }
      },
      boxShadow: {
        soft: '0 18px 50px rgba(139, 38, 30, 0.08)'
      }
    }
  },
  plugins: []
}

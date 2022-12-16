/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        './pages/**/*.{js,ts,jsx,tsx}',
        './components/**/*.{js,ts,jsx,tsx}',
        './app/**/*.{js,ts,jsx,tsx}',
    ],
    theme: {
        container: {
            center: true,
        },
        extend: {},
    },
    plugins: [],
    corePlugins: {
        // Necessary to remove errors with antd
        preflight: false
    },
    important:true,
}
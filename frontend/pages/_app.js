import '../styles/globals.css'
import { AuthenticationProvider } from '../lib/AuthenticationProvider';
import { ConfigProvider } from 'antd';
import Sidebar from '../components/Sidebar';


function App({ Component, pageProps: { ...pageProps }
}) {
    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: '#192B4F',
                    colorFillSecondary: '#3083DC',
                    colorBgContainer: '#F7F7FF'
                },
            }}
        >
            <AuthenticationProvider>
                <Sidebar>
                    <Component {...pageProps} />
                </Sidebar>
            </AuthenticationProvider>
        </ConfigProvider>
    );
}

export default App;
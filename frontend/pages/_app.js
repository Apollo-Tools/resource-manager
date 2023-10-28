import '../styles/globals.css';
import {AuthenticationProvider} from '../lib/misc/AuthenticationProvider';
import {ConfigProvider} from 'antd';
import Sidebar from '../components/misc/Sidebar';
import PropTypes from 'prop-types';
import Script from 'next/script';


const App = ({Component, pageProps: {...pageProps}}) => {
  return (
    <>
      <Script src="/__ENV.js" strategy="beforeInteractive" />
      <ConfigProvider
        theme={{
          token: {
            colorPrimary: '#192B4F',
            colorFillSecondary: 'rgba(0, 199, 255, 0.25)',
            colorPrimaryBg: '#00C7FF1A',
            colorPrimaryBgHover: '#00C7FF33',
          },
        }}
      >
        <AuthenticationProvider>
          <Sidebar >
            <Component {...pageProps} />
          </Sidebar>
        </AuthenticationProvider>
      </ConfigProvider>
    </>
  );
};

App.propTypes = {
  Component: PropTypes.func.isRequired,
  pageProps: PropTypes.object.isRequired,
};

export default App;

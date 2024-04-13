import '../styles/globals.css';
import {AuthenticationProvider} from '../lib/misc/AuthenticationProvider';
import {ConfigProvider, App as AntApp, notification} from 'antd';
import Sidebar from '../components/misc/Sidebar';
import PropTypes from 'prop-types';
import Script from 'next/script';
import {useEffect, useState} from 'react';
import {openNotification} from '../components/misc/ErrorNotification';


const App = ({Component, pageProps: {...pageProps}}) => {
  const [api, contextHolder] = notification.useNotification();
  const [error, setError] = useState();

  useEffect(() => {
    if (error) {
      openNotification(api, error.message);
      setError(null);
    }
  }, [error]);

  return (
    <AntApp>
      {contextHolder}
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
          <Sidebar>
            <Component {...pageProps} error={error} setError={setError}/>
          </Sidebar>
        </AuthenticationProvider>
      </ConfigProvider>
    </AntApp>
  );
};

App.propTypes = {
  Component: PropTypes.func.isRequired,
  pageProps: PropTypes.object.isRequired,
};

export default App;

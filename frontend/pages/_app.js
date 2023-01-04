import '../styles/globals.css';
import {AuthenticationProvider} from '../lib/AuthenticationProvider';
import {ConfigProvider} from 'antd';
import Sidebar from '../components/Sidebar';
import PropTypes from 'prop-types';


const App = ({Component, pageProps: {...pageProps}}) => {
  return (
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
          <Component {...pageProps} />
        </Sidebar>
      </AuthenticationProvider>
    </ConfigProvider>
  );
};

App.propTypes = {
  Component: PropTypes.func.isRequired,
  pageProps: PropTypes.object.isRequired,
};

export default App;

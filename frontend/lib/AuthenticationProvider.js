import {createContext, useContext, useEffect, useState} from 'react';
import {useRouter} from 'next/router';
import PropTypes from 'prop-types';

const AuthContext = createContext(null);

const getTokenFromStorage = () => {
  const token = localStorage.getItem('jwt');
  console.log('local storage: ' + token);
  return token;
};

const storeTokenToStorage = (jwt) => {
  return localStorage.setItem('jwt', jwt);
};

const decodeTokenPayload = (encodedToken) => {
  const tokenParts = encodedToken.split('.');
  if (tokenParts.length !== 3) {
    return null;
  }
  return JSON.parse(Buffer.from(tokenParts[1], 'base64').toString());
};

export const AuthenticationProvider = ({children}) => {
  const [token, setToken] = useState('');
  const [payload, setPayload] = useState(null);
  const [newToken, setNewToken] = useState(null);
  const [isAuthenticated, setAuthenticated] = useState(false);
  const [isInitialised, setInitialised] = useState(false);
  const router = useRouter();

  useEffect(() => {
    console.log('Get initial token');
    const jwtToken = getTokenFromStorage();
    if (jwtToken != null && jwtToken !== '') {
      const decodedPayload = decodeTokenPayload(jwtToken);
      if (decodedPayload == null) {
        setAuthenticated(false);
        return;
      }
      setToken(() => jwtToken);
      setPayload(() => {
        console.log('decode payload ' + decodedPayload);
        return decodedPayload;
      });
    }
    setInitialised(() => true);
  }, []);

  useEffect(() => {
    if (token != null && payload != null) {
      setAuthenticated(() => {
        if (token === '') {
          return false;
        } else if (payload.exp < new Date() / 1000) {
          console.log('token' + token);
          return false;
        }
        console.log('token changed: ' + token);
        return true;
      });
    }
  }, [token]);

  useEffect(() => {
    if (newToken != null) {
      console.log('new token');
      const decodedPayload = decodeTokenPayload(newToken);
      if (decodedPayload == null) {
        setAuthenticated(false);
        return;
      }
      storeTokenToStorage(newToken);
      setToken(() => newToken);
      setPayload(() => decodedPayload);
    }
  }, [newToken]);

  useEffect(() => {
    if (!isAuthenticated && !router.pathname.endsWith('/login') && !router.pathname.endsWith('/signup')) {
      router.push('/accounts/login');
    } else if (isAuthenticated) {
      router.replace('/');
    }
  }, [isAuthenticated]);

  const logout = () => {
    setToken('');
    setAuthenticated(false);
    setPayload(null);
    storeTokenToStorage('');
  };

  const loginUser = (newToken) => {
    setNewToken(newToken);
  };

  const checkTokenExpired = () => {
    if (!payload) {
      return true;
    }
    if (payload.exp < new Date() / 1000) {
      console.log('token expired');
      logout();
      return true;
    }
    return false;
  };

  return (
    <AuthContext.Provider value={ {payload, token, isAuthenticated, loginUser, logout, checkTokenExpired} }>
      { isInitialised && children }
    </AuthContext.Provider>
  );
};

AuthenticationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export const useAuth = () => useContext(AuthContext);

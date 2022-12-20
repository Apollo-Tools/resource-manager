import React, { useContext, createContext, useState, useEffect } from 'react';
import LoginForm from '../pages/accounts/login';

const AuthContext = createContext(null);

const getTokenFromStorage = () => {
    let token = localStorage.getItem('jwt');
    console.log("local storage: " + token);
    return token;
}

const storeTokenToStorage = (jwt) => {
    return localStorage.setItem('jwt', jwt);
}

const decodeTokenPayload = (encodedToken) => {
    let tokenParts = encodedToken.split('.');
    if (tokenParts.length !== 3) {
        return null;
    }
    return Buffer.from(tokenParts[1],'base64').toString();
}

export const AuthenticationProvider = ({ children }) => {
    const [token, setToken] = useState('');
    const [payload, setPayload] = useState(null);
    const [newToken, setNewToken] = useState(null);
    const [isAuthenticated, setAuthenticated] = useState(false);
    const [isInitialised, setInitialised] = useState(false);

    useEffect(() => {
        console.log("Get initial token")
        let jwtToken = getTokenFromStorage();
        if(jwtToken != null && jwtToken !== '') {
            let decodedPayload = decodeTokenPayload(jwtToken);
            if (decodedPayload == null) {
                setAuthenticated(false);
                return;
            }
            setToken(() => jwtToken);
            setPayload(() => {
                console.log("decode payload " + decodedPayload);
                return decodedPayload;
            });
        }
        setInitialised(() => true);
    }, []);

    useEffect(() => {
        setAuthenticated(() => {
            if (token == null || token === '') {
                return false
            }
            else if (payload == null || payload.exp < new Date()/1000) {
                console.log("token" + token)
                return false
            }
            console.log("token changed: " + token);
            return true;
        });
    }, [token])

    useEffect(() => {
        if (newToken != null) {
            console.log("new token");
            let decodedPayload = decodeTokenPayload(newToken);
            if (decodedPayload == null) {
                setAuthenticated(false);
                return;
            }
            storeTokenToStorage(newToken);
            setToken(() => newToken);
            setPayload(() => decodedPayload);
        }
    }, [newToken]);

    const logout = () => {
        setToken('');
        setAuthenticated(false);
        setPayload(null);
        storeTokenToStorage('');
    }

    return (
        <AuthContext.Provider value={{token, isAuthenticated, setNewToken, logout}}>
            {isInitialised && (isAuthenticated ? children : <LoginForm />)}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)
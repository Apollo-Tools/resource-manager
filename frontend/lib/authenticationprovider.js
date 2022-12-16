import React, { useContext, createContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthenticationProvider = ({ children }) => {
    const [auth, setAuth] = useState(null)
    const [token, setToken] = useState('')

    useEffect(() => {
        const authData = {
            token: token,
            isAuthenticated: true,
        }
        //setAuth(authData)
        console.log(authData)
        console.log(token)
    }, [token])

    return (
        <AuthContext.Provider value={{token, setToken}}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)
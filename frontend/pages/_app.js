import '../styles/globals.css'
import { AuthenticationProvider, AuthContext } from '../lib/authenticationprovider';
import { useState } from 'react';

const unauthenticated = {
    token: '',
    isAuthenticated: false,
    username: '',
}


function App({ Component, pageProps: { ...pageProps }
}) {
    const [authentication, setAuthentication] = useState(unauthenticated);



    return (
        <AuthenticationProvider>
            <Component {...pageProps} />
        </AuthenticationProvider>
    );
}

export default App;
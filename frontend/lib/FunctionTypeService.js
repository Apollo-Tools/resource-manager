import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/function-types`;

/**
 * List all function types.
 *
 * @param {string} token the access token
 * @param {function} setFunctionTypes the function to set the retrieved function types
 * @param {function} setError the function to set the error if one occurred
 */
export async function listFunctionTypes(token, setFunctionTypes, setError) {
    try {
        const response = await fetch(`${API_ROUTE}`, {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
        const data = await response.json();
        setFunctionTypes(() => data);
    } catch (error) {
        setError(true);
        console.log(error);
    }
}

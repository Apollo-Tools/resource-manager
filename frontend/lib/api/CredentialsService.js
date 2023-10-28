import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/credentials`;

/**
 * Create new credentials for a provider.
 *
 * @param {number} providerId the id of the provider
 * @param {string} accessKey the access key
 * @param {string} secretAccessKey the secret access key
 * @param {string} sessionToken the session token
 * @param {string} token the access token
 * @param {function} setCredentials the function to set the created credentials
 * @param {function} setError the function to set the error if one occurs
 */
export async function createCredentials(providerId, accessKey, secretAccessKey, sessionToken, token,
    setCredentials, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        access_key: accessKey,
        resource_provider: {
          provider_id: providerId,
        },
        secret_access_key: secretAccessKey,
        session_token: sessionToken,
      }),
    });
    const data = await response.json();
    setCredentials(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get list of all cloud credentials of the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setCredentials the function to set the retrieved credentials
 * @param {function} setError the function to set the error if one occurs
 */
export async function listCredentials(token, setCredentials, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setCredentials(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete existing cloud credentials.
 *
 * @param {string} id the id of the credentials
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteCredentials(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

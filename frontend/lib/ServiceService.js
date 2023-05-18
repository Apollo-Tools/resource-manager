import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/services`;

/**
 * Create a new service.
 *
 * @param {string} name the name of the service
 * @param {string} token the access token
 * @param {function} setService the function to set the created service
 * @param {function} setError the function to set the error if one occurred
 */
export async function createService(name, token, setService, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
      }),
    });
    const data = await response.json();
    setService(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all existing services.
 *
 * @param {string} token the access token
 * @param {function} setServices the function to set the retrieved services
 * @param {function} setError the function to set the error if one occurred
 */
export async function listServices(token, setServices, setError) {
  try {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setServices(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing service.
 *
 * @param {number} id the id of the service
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteService(id, token, setError) {
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

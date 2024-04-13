import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/accounts`;

/**
 * List all namespaces of the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setNamespaces the function to set the retrieved namespaces
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyNamespaces(token, setNamespaces, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/me/k8snamespaces`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setNamespaces);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all namespaces of the selected account.
 *
 * @param {number} accountId the id of the account
 * @param {string} token the access token
 * @param {function} setNamespaces the function to set the retrieved namespaces
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listNamespaces(accountId, token, setNamespaces, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${accountId}/k8snamespaces`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setNamespaces);
  };
  await handleApiCall(apiCall, setLoading, setError);
}


/**
 * Add namespace to account.
 *
 * @param {number} accountId the id of the account
 * @param {any[]} namespaceId the id of the namespace
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function addAccountNamespace(accountId, namespaceId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${accountId}/k8snamespaces/${namespaceId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    return response.ok;
  };
  return await handleApiCall(apiCall, setLoading, setError);
}


/**
 * Remove a namespace from an account.
 *
 * @param {number} accountId the id of the account
 * @param {boolean} namespaceId the id of the namespace
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteNamespaceFromAccount(accountId, namespaceId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${accountId}/k8snamespaces/${namespaceId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.ok;
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

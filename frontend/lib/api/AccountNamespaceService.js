import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/accounts`;

/**
 * List all namespaces of the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setNamespaces the function to set the retrieved namespaces
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyNamespaces(token, setNamespaces, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/me/k8snamespaces`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setNamespaces(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all namespaces of the selected accountg.
 *
 * @param {number} accountId the id of the account
 * @param {string} token the access token
 * @param {function} setNamespaces the function to set the retrieved namespaces
 * @param {function} setError the function to set the error if one occurred
 */
export async function listNamespaces(accountId, token, setNamespaces, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${accountId}/k8snamespaces`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setNamespaces(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}


/**
 * Add namespace to account.
 *
 * @param {number} accountId the id of the account
 * @param {any[]} namespaceId the id of the namespace
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 */
export async function addAccountNamespace(accountId, namespaceId, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${accountId}/k8snamespaces/${namespaceId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}


/**
 * Remove a namespace from an account.
 *
 * @param {number} accountId the id of the account
 * @param {boolean} namespaceId the id of the namespace
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteNamespaceFromAccount(accountId, namespaceId, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${accountId}/k8snamespaces/${namespaceId}`, {
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

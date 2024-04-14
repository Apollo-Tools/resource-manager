import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/ensembles`;

/**
 * Create a new ensemble.
 *
 * @param {string} name the name of the ensemble
 * @param {object} slos the service level objectives
 * @param {list} resources the resources
 * @param {string} token the access token
 * @param {function} setEnsemble the function to set the created ensemble
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createEnsemble(name, slos, resources, token, setEnsemble, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        slos: slos,
        resources: resources,
      }),
    });
    await setResult(response, setEnsemble);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all existing ensembles.
 *
 * @param {string} token the access token
 * @param {function} setEnsembles the function to set the retrieved ensembles
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listEnsembles(token, setEnsembles, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setEnsembles);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get the details of one ensemble.
 *
 * @param {number} id the id of the ensemble
 * @param {string} token the access token
 * @param {function} setEnsemble the function to set the retrieved ensemble
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function getEnsemble(id, token, setEnsemble, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setEnsemble);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing ensemble.
 *
 * @param {number} id the id of the ensemble
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteEnsemble(id, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Validate an existing ensemble for SLO breaches.
 *
 * @param {number} id the id of the ensemble
 * @param {string} token the access token
 * @param {function} setValidationData the function to set the validation data
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if all resources are valid else false
 */
export async function validateEnsemble(id, token, setValidationData, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}/validate`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await checkResponseOk(response);
    const data = await response.json();
    setValidationData?.(data);
    return data.map((result) => result.is_valid).reduce((prev, current) => prev && current);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

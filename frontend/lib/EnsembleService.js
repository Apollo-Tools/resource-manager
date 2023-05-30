import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/ensembles`;

/**
 * Create a new ensemble.
 *
 * @param {string} name the name of the ensemble
 * @param {object} slos the service level objectives
 * @param {list} resources the resources
 * @param {string} token the access token
 * @param {function} setEnsemble the function to set the created ensemble
 * @param {function} setError the function to set the error if one occurs
 */
export async function createEnsemble(name, slos, resources, token, setEnsemble, setError) {
  try {
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
    const data = await response.json();
    setEnsemble(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all existing ensembles.
 *
 * @param {string} token the access token
 * @param {function} setEnsembles the function to set the retrieved ensembles
 * @param {function} setError the function to set the error if one occurs
 */
export async function listEnsembles(token, setEnsembles, setError) {
  try {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setEnsembles(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get the details of one ensemble.
 *
 * @param {number} id the id of the ensemble
 * @param {string} token the access token
 * @param {function} setEnsemble the function to set the retrieved ensemble
 * @param {function} setError the function to set the error if one occurs
 */
export async function getEnsemble(id, token, setEnsemble, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setEnsemble(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing ensemble.
 *
 * @param {number} id the id of the ensemble
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteEnsemble(id, token, setError) {
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

/**
 * Validate an existing ensemble for SLO breaches.
 *
 * @param {number} id the id of the ensemble
 * @param {string} token the access token
 * @param {function} setValidationData the function to set the validation data
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<List<*>>} a list of that contains a validation entry for each resource
 */
export async function validateEnsemble(id, token, setValidationData, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/validate`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    console.log(data);
    setValidationData?.(data);
    return data.map((result) => result.is_valid).reduce((prev, current) => prev && current);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

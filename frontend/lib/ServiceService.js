import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/services`;

/**
 * Create a new service.
 *
 * @param {string} name the name of the service
 * @param {string} image the name of the image
 * @param {number} replicas the amount of replicas
 * @param {string[]} ports the ports to expose
 * @param {number} cpu the necessary cpu ressources
 * @param {number} memory the necessary memory
 * @param {number} serviceTypeId the service type
 * @param {string} token the access token
 * @param {function} setService the function to set the created service
 * @param {function} setError the function to set the error if one occurred
 */
export async function createService(name, image, replicas, ports, cpu, memory,
    serviceTypeId, token, setService, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        image: image,
        replicas: replicas,
        ports: ports,
        cpu: cpu,
        memory: memory,
        service_type: {
          service_type_id: serviceTypeId,
        },
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
 * Get the details of a service.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setService the function to set the retrieved service
 * @param {function} setError the function to set the error if one occurred
 */
export async function getService(id, token, setService, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setService(() => data);
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

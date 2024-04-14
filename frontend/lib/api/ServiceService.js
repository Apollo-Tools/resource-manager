import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/services`;

/**
 * Create a new service.
 * *
 * @param {number} serviceTypeId the service type
 * @param {string} name the name of the service
 * @param {string} image the name of the image
 * @param {number} replicas the amount of replicas
 * @param {string[]} ports the ports to expose
 * @param {number} cpu the necessary cpu resources
 * @param {number} memory the necessary memory
 * @param {number} k8sServiceTypeId the k8s service type
 * @param {object[]} envVars the environment variables to set
 * @param {object[]} volumeMounts the volume mounts for the service
 * @param {boolean} isPublic whether the public should be publicly available
 * @param {string} token the access token
 * @param {function} setService the function to set the created service
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createService(serviceTypeId, name, image, replicas, ports, cpu, memory,
    k8sServiceTypeId, envVars, volumeMounts, isPublic, token, setService, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        service_type: {
          artifact_type_id: serviceTypeId,
        },
        name: name,
        image: image,
        replicas: replicas,
        ports: ports,
        cpu: cpu,
        memory: memory,
        k8s_service_type: {
          service_type_id: k8sServiceTypeId,
        },
        env_vars: envVars,
        volume_mounts: volumeMounts,
        is_public: isPublic,
      }),
    });
    await setResult(response, setService);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Update an existing service.
 *
 * @param {number} id the id of the service
 * @param {number} replicas the amount of replicas
 * @param {string[]} ports the ports to expose
 * @param {number} cpu the necessary cpu resources
 * @param {number} memory the necessary memory
 * @param {number} k8sServiceTypeId the service type
 * @param {object[]} envVars the environment variables to set
 * @param {object[]} volumeMounts the volume mounts for the service
 * @param {boolean} isPublic whether the public should be publicly available
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateService(id, replicas, ports, cpu, memory,
    k8sServiceTypeId, envVars, volumeMounts, isPublic, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        replicas: replicas,
        ports: ports,
        cpu: cpu,
        memory: memory,
        k8s_service_type: {
          service_type_id: k8sServiceTypeId,
        },
        env_vars: envVars.map((envVar) => {
          delete envVar.env_var_id;
          delete envVar.created_at;
          return envVar;
        }),
        volume_mounts: volumeMounts.map((volumeMount) => {
          delete volumeMount.volume_mount_id;
          delete volumeMount.created_at;
          return volumeMount;
        }),
        is_public: isPublic,
      }),
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all owned services.
 *
 * @param {string} token the access token
 * @param {function} setServices the function to set the retrieved services
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyServices(token, setServices, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/personal`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setServices);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all accessible services.
 *
 * @param {string} token the access token
 * @param {function} setServices the function to set the retrieved services
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listAllServices(token, setServices, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(API_ROUTE, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setServices);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get the details of a service.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setService the function to set the retrieved service
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function getService(id, token, setService, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setService);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing service.
 *
 * @param {number} id the id of the service
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteService(id, token, setLoading, setError) {
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

import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/reservations`;

/**
 * List all reservations of the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setReservations the function to set the retrieved reservations
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyReservations(token, setReservations, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setReservations(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all logs from a reservation
 *
 * @param {number} reservationId the id of the reservation
 * @param {string} token the access token
 * @param {function} setReservationLogs the function to set the retrieved logs
 * @param {function} setError the function to set the error if one occurred
 */
export async function listReservationLogs(reservationId, token, setReservationLogs, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${reservationId}/logs`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setReservationLogs(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Reserve resources for deployment.
 *
 * @param {object} requestBody the request body
 * @param {string} token the access token
 * @param {function} setNewReservation the function to set the created reservation
 * @param {function} setError the function to set the error if one occurred
 */
export async function reserveResources(
    requestBody,
    token,
    setNewReservation,
    setError,
) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    });
    const data = await response.json();
    setNewReservation(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get the details of a reservation.
 *
 * @param {number} id the id of the reservation
 * @param {string} token the access token
 * @param {function} setReservation the function to set the reservation
 * @param {function} setError the function to set the error if one occurred
 */
export async function getReservation(id, token, setReservation, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setReservation(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Cancel an existing reservation
 *
 * @param {number} id the number of the reservation
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function cancelReservation(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/cancel`, {
      method: 'PATCH',
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

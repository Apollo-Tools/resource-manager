/**
 * Helper function to update the loading state
 * The state must be an object of key - value pairs, where the key is the
 * type and the value is the current state.
 *
 * @param {function} setLoading the function to update the state
 * @param {string} type the type of the state to change
 * @param {boolean} newState the new state
 */
function updateLoading(setLoading, type, newState) {
  setLoading((prevState) => {
    const newLoadings = {...prevState};
    newLoadings[type] = newState;
    return newLoadings;
  });
}

/**
 * Retrieve a helper function to update the loading state by
 * providing the update function of the loading state.
 *
 * @param {string} type the type of the state to change
 * @param {function} setLoading the function to update the state
 * @return {function} a helper function to update the loading state of the given type
 */
export function updateLoadingState(type, setLoading) {
  return (newState) => updateLoading(setLoading, type, newState);
}


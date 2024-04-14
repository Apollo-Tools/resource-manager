export const nameValidationRule = {
  required: true,
  message: 'Please provide a name!',
};

export const nameRegexValidationRule = {
  pattern: /^[a-z0-9]+$/,
  message: 'Name must only contain lowercase letters and digits!',
};

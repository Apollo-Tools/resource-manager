export const notificationProvider = (notification, message) => {
  notification.warning({
    message: `This did not work`,
    description: message,
    placement: 'top',
  });
};

export const successNotification = (notification, message) => {
  notification.success({
    message: `Success`,
    description: message,
    placement: 'top',
  });
};

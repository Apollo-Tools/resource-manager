export const openNotification = (notification, message) => {
  notification.warning({
    message: `This did not work`,
    description: message,
    placement: 'top',
  });
};

import PropTypes from 'prop-types';
import {format} from 'date-fns-tz';

const DateFormatter = ({dateTimestamp, includeTime}) => {
  const dateTime = new Date(dateTimestamp);
  if (includeTime) {
    return <div>{format(dateTime, 'yyyy-MM-dd HH:mm:ss')}</div>;
  }
  return <div>{format(dateTime, 'yyyy-MM-dd')}</div>;
};

DateFormatter.propTypes = {
  dateTimestamp: PropTypes.number.isRequired,
  includeTime: PropTypes.bool,
};

export default DateFormatter;

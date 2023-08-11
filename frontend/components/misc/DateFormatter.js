import PropTypes from 'prop-types';
import {format} from 'date-fns-tz';

const DateFormatter = ({dateTimestamp, includeTime}) => {
  const dateTime = new Date(dateTimestamp)
  if (includeTime) {
    return <time dateTime={dateTime}>{format(dateTime, 'yyyy-MM-dd HH:mm:ss')}</time>;
  }
  return <time dateTime={dateTime}>{format(dateTime, 'yyyy-MM-dd')}</time>;
};

DateFormatter.propTypes = {
  dateTimestamp: PropTypes.number.isRequired,
  includeTime: PropTypes.bool,
};

export default DateFormatter;

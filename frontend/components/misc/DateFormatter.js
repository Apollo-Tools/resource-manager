import PropTypes from 'prop-types';
import {format} from 'date-fns';

const DateFormatter = ({dateTimestamp, includeTime}) => {
  if (includeTime) {
    return <time dateTime={dateTimestamp}>{format(new Date(dateTimestamp), 'yyyy-MM-dd hh:mm:ss')}</time>;
  }
  return <time dateTime={dateTimestamp}>{format(new Date(dateTimestamp), 'yyyy-MM-dd')}</time>;
};

DateFormatter.propTypes = {
  dateTimestamp: PropTypes.number.isRequired,
  includeTime: PropTypes.bool,
};

export default DateFormatter;

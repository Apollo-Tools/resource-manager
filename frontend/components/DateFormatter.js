import PropTypes from 'prop-types';
import {format} from 'date-fns';

const DateFormatter = ({dateTimestamp}) => {
  return <time dateTime={dateTimestamp}>{format(new Date(dateTimestamp), 'yyyy-MM-dd')}
  </time>;
};

DateFormatter.propTypes = {
  dateTimestamp: PropTypes.number,
};

export default DateFormatter;

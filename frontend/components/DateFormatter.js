import PropTypes from 'prop-types';
import {format} from 'date-fns';

const DateFormatter = ({dateString}) => {
  return <time dateTime={
    dateString}>{format(new Date(dateString), 'yyyy-MM-dd')}
  </time>;
};

DateFormatter.propTypes = {
  dateString: PropTypes.string,
};

export default DateFormatter;

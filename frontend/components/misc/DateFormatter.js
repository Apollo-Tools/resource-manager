import PropTypes from 'prop-types';
import {utcToZonedTime, format} from 'date-fns-tz';

const DateFormatter = ({dateTimestamp, includeTime}) => {
  // eslint-disable-next-line new-cap
  const localTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const zonedTimestamp = utcToZonedTime(new Date(Date.UTC(1970, 0, 1) + dateTimestamp),
      localTimezone);
  if (includeTime) {
    return <time dateTime={zonedTimestamp}>{format(zonedTimestamp, 'yyyy-MM-dd HH:mm:ss')}</time>;
  }
  return <time dateTime={zonedTimestamp}>{format(zonedTimestamp, 'yyyy-MM-dd')}</time>;
};

DateFormatter.propTypes = {
  dateTimestamp: PropTypes.number.isRequired,
  includeTime: PropTypes.bool,
};

export default DateFormatter;

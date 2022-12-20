import { format } from 'date-fns';

const DateFormatter = ({ dateString }) => {
    return <time dateTime={dateString}>{format(new Date(dateString), 'yyyy-MM-dd')}</time>;
}

export default DateFormatter;
import DateFormatter from './DateFormatter';
import {Tooltip} from 'antd';
import PropTypes from 'prop-types';


const DateColumnRender = ({value}) => {
  return (
    <Tooltip
      placement="top"
      title={<DateFormatter dateTimestamp={value} includeTime/>}
      arrow={true}
      color={'#262626'}
      overlayClassName="w-fit max-w-sm text-center"
      overlayInnerStyle={{textAlign: 'center', padding: '6px'}}
    >
      <div>
        <DateFormatter dateTimestamp={value}/>
      </div>
    </Tooltip>
  );
};

DateColumnRender.propTypes = {
  value: PropTypes.number.isRequired,
};
export default DateColumnRender;

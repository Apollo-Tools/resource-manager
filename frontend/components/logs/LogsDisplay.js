import {Collapse} from 'antd';
import DateFormatter from '../misc/DateFormatter';
import PropTypes from 'prop-types';


const LogsDisplay = ({logs}) => {
  if (logs.length === 0) {
    return <>Currently there are no logs...</>;
  }

  const collapseItems = logs.map((log) => {
    return {
      key: log.log_id,
      label: <DateFormatter dateTimestamp={log.created_at} includeTime/>,
      children: <div className="max-h-56 overflow-auto m-[-12px] px-2">{log.log_value}</div>,
    };
  });

  return <Collapse accordion style={{whiteSpace: 'pre-wrap'}} size="small" bordered={false} items={collapseItems} />;
};

LogsDisplay.propTypes = {
  logs: PropTypes.arrayOf(PropTypes.shape({
    log_id: PropTypes.number.isRequired,
    log_value: PropTypes.string.isRequired,
    created_at: PropTypes.number.isRequired,
  }),
  ).isRequired,
};

export default LogsDisplay;

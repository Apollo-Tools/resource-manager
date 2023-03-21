import {Collapse, Typography} from 'antd';
import DateFormatter from '../misc/DateFormatter';
import PropTypes from 'prop-types';
const {Panel} = Collapse;


const LogsDisplay = ({logs}) => {
  if (logs.length === 0) {
    return <Typography.Title level={4}>Currently there are no logs...</Typography.Title>;
  }

  return <Collapse accordion style={{whiteSpace: 'pre-wrap'}}>
    {logs.map((log) =>
      (<Panel header={<DateFormatter dateTimestamp={log.created_at} includeTime/>} key={log.log_id}>
        {log.log_value}
      </Panel>))
    }
  </Collapse>;
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

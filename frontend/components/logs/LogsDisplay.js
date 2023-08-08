import {Collapse} from 'antd';
import DateFormatter from '../misc/DateFormatter';
import PropTypes from 'prop-types';
const {Panel} = Collapse;


const LogsDisplay = ({logs}) => {
  if (logs.length === 0) {
    return <>Currently there are no logs...</>;
  }

  return <Collapse accordion style={{whiteSpace: 'pre-wrap'}} size="small">
    {logs.map((log) =>
      (<Panel header={<DateFormatter dateTimestamp={log.created_at} includeTime/>} key={log.log_id}>
        <div className="max-h-56 overflow-auto m-[-12px] px-2">
          {log.log_value}
        </div>
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

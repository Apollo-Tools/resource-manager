import PropTypes from 'prop-types';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import {CheckCircleTwoTone, CloseCircleTwoTone} from '@ant-design/icons';


const ResourceDetailsCard = ({resource}) => {
  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <TextDataDisplay label="Resource Type" value={resource.resource_type.resource_type} className="col-span-6"/>
      <TextDataDisplay label="Provider" value={resource.region.resource_provider.provider} className="col-span-6" />
      <TextDataDisplay label="Region" value={resource.region.name} className="col-span-6" />
      <TextDataDisplay label="Is self managed?" value={
        <>{resource.is_self_managed ? <CheckCircleTwoTone /> :
          <CloseCircleTwoTone />} {resource.is_self_managed.toString()}
        </>
      } className="col-span-6"/>
      <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={resource.created_at} />} className="col-span-6"/>
    </div>
  );
};

ResourceDetailsCard.propTypes = {
  resource: PropTypes.object.isRequired,
};

export default ResourceDetailsCard;

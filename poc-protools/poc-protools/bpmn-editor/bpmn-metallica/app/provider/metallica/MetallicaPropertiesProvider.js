import implementationProps from './parts/MetallicaProps';

import { is } from 'bpmn-js/lib/util/ModelUtil';

const LOW_PRIORITY = 500;

export default function MetallicaPropertiesProvider(propertiesPanel, translate) {

  // API ////////
  this.getGroups = function(element) {
    return function(groups) {

      if(is(element, 'bpmn:ServiceTask')) {
        groups.push(createMetallicaGroup(element, translate));
      }

      return groups;
    }
  };


  // registration ////////
  propertiesPanel.registerProvider(LOW_PRIORITY, this);
}

MetallicaPropertiesProvider.$inject = [ 'propertiesPanel', 'translate' ];

function createMetallicaGroup(element, translate) {

  const metallicaGroup = {
    id: 'Metallica',
    label: translate('Metallica properties'),
    entries: implementationProps(element)
  };

  return metallicaGroup
}

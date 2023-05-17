import {python} from '@codemirror/lang-python';

/**
 * Get the suitable editor extension for the functions editor.
 *
 * @param {string} runtimeName the name of the runtime
 * @return {null|LanguageSupport} the editor extension if available else null
 */
export function getEditorExtension(runtimeName) {
  if (runtimeName.toLowerCase().startsWith('python')) {
    return python();
  }
  return null;
}

import React from 'react';
import {Tabs, Tab} from 'material-ui/Tabs';
// From https://github.com/oliviertassinari/react-swipeable-views
import SwipeableViews from 'react-swipeable-views';
import MaterialUIAutocomplete from './ExamplesChooser';
import injectTapEventPlugin from 'react-tap-event-plugin';
import SingleProvenanceCard from './ProvenanceChooser';
import Paper from 'material-ui/Paper';

injectTapEventPlugin();
const styles = {
  headline: {
    fontSize: 24,
    paddingTop: 16,
    marginBottom: 12,
    fontWeight: 400,
  },
  slide: {
    padding: 10,
  },
  div: {
    padding: 100,
    margin: 100,
  },
};

export default class TabsExampleSwipeable extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      slideIndex: 0,
      ChosenNodes: [],
    };
  }


  handleChange = (value) => {
    this.setState({
      slideIndex: value,
      
    });
  };

handleChosenNodes = (value,nodes) => {
    this.setState({
      
      ChosenNodes: nodes,
    });
    this.handleChange(value);
  };

  render() {
    return (
      <div>
        <Tabs
          onChange={this.handleChange}
          value={this.state.slideIndex}>
          <Tab label="Select Examples" value={0} />
          <Tab label="Explain" value={1} />
          <Tab label="Query" value={2} />
        </Tabs>
        <SwipeableViews
          index={this.state.slideIndex}
          onChangeIndex={this.handleChange}>
          <div>
          <div style={styles.div}>
            <MaterialUIAutocomplete switchTab={this.handleChosenNodes} />
          </div>
          </div>
          <div>
          <div style={styles.slide}>
          <SingleProvenanceCard nodes={this.state.ChosenNodes}
          switchTab={this.handleChange}/>
          </div>
          </div>
          <div style={styles.slide}>
            slide n°3
          </div>
        </SwipeableViews>
      </div>
    );
  }
}
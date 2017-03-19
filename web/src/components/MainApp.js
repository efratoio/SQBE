import React from 'react';
import {Tabs, Tab} from 'material-ui/Tabs';
// From https://github.com/oliviertassinari/react-swipeable-views
import SwipeableViews from 'react-swipeable-views';
import MaterialUIAutocomplete from './ExamplesChooser';
import injectTapEventPlugin from 'react-tap-event-plugin';
import ExplanationChooser from './ExplanationChooser';
import DiffChooser from "./DiffChooser";
import QueryPaper from './QueryPresentor';
import Paper from 'material-ui/Paper';
import {
  Step,
  Stepper,
  StepLabel,
} from 'material-ui/Stepper';
import RaisedButton from 'material-ui/RaisedButton';
import FlatButton from 'material-ui/FlatButton';

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
    padding: 10,
    margin: 10,
  },
};

export default class TabsExampleSwipeable extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      slideIndex: 0,
      ChosenNodes: [],
      finished: false,
    stepIndex: 0,
    showQuery: false,
    renedrDiff:false,
    };
  }


  handleChange = (value) => {
    this.handleNext();
  };


  handleNext = () => {
    const stepIndex = this.state.stepIndex;
    if(!this.state.finished)
      this.setState({
        stepIndex: stepIndex + 1,
        finished: stepIndex >= 3,
      });
  };

  handlePrev = () => {
    const stepIndex = this.state.stepIndex;
    if (stepIndex > 0) {
      this.setState({stepIndex: stepIndex - 1});
    }
  };

  getStepContent(stepIndex) {
    switch (stepIndex) {
      case 0:
        return 'Select the items you want to get';
      case 1:
        return 'Add explanations for each item';
      case 2:
        return 'Answer some questions';
      case 3:
        return 'This is the query';
      default:
        return 'You\'re a long way from home sonny jim!';
    }
  }

handleChosenNodes = (nodes) => {
    this.setState({
      
      ChosenNodes: nodes,
    });
    this.handleNext();
  };
handleExpalantionsDone = () => {
  this.handleNext();
  this.setState({renederDiff: true});

};
handleQuestionsDone = () => {
  this.setState({showQuery: true});
  this.handleNext();
};
  render() {
    return (
      <div>
      <Stepper activeStep={this.state.stepIndex}>
        <Step>
            <StepLabel>Choose examples</StepLabel>
          </Step>
          <Step>
            <StepLabel>Add explanations</StepLabel>
          </Step>
          <Step>
            <StepLabel>Answer some question</StepLabel>
          </Step>
          <Step>
            <StepLabel>Get the query</StepLabel>
          </Step>
        </Stepper>
        <SwipeableViews
          index={this.state.stepIndex}
          onChangeIndex={this.handleChange}>
          <div>
          <div style={styles.slide}>
            <MaterialUIAutocomplete switchTab={this.handleChosenNodes} />
          </div>
          </div>
          <div>
          <div style={styles.slide}>
          <ExplanationChooser nodes={this.state.ChosenNodes} handleDone={this.handleExpalantionsDone}/>
          </div>
          </div>
          <div>
          <div style={styles.slide}>
          <DiffChooser render={this.state.renederDiff} nodes={this.state.ChosenNodes} handleDone={this.handleQuestionsDone}/>
          </div>
          </div>
          <div>
          <div style={styles.slide}>
         <QueryPaper demi={this.state.showQuery}/>
          </div>
          </div>
        </SwipeableViews>
      </div>
    );
  }
}
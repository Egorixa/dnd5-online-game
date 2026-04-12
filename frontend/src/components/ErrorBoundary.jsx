// React Error Boundary для перехвата ошибок рендера.
import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  handleRetry = () => {
    this.setState({ hasError: false });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <h3>Что-то пошло не так</h3>
          <p>Произошла ошибка при отображении этого раздела.</p>
          <button className="btn-secondary" onClick={this.handleRetry}>
            Попробовать снова
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

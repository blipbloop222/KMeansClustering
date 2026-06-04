//Reducer for benchmark session state: run history, latest results, per-algo status, errors, backend flag.

export const initialAlgoStatus = {
  sequential: 'idle',
  concurrent: 'idle',
  parallel: 'idle',
}

export const initialBenchmarkState = {
  runHistory: [],
  currentResults: {},
  algoStatus: { ...initialAlgoStatus },
  error: null,
  backendOnline: null,
}

export function benchmarkReducer(state, action) {
  switch (action.type) {
    case 'RESET_RUN':
      return {
        ...state,
        currentResults: {},
        algoStatus: { ...initialAlgoStatus },
        error: null,
      }
    case 'SET_ALGO_STATUS':
      return {
        ...state,
        algoStatus: { ...state.algoStatus, [action.algo]: action.status },
      }
    case 'SET_RESULT':
      return {
        ...state,
        currentResults: { ...state.currentResults, [action.algo]: action.result },
      }
    case 'SET_ERROR':
      return { ...state, error: action.error }
    case 'ADD_HISTORY':
      return {
        ...state,
        runHistory: [...state.runHistory, action.entry],
        currentResults: action.entry.results,
      }
    case 'CLEAR_HISTORY':
      return { ...state, runHistory: [] }
    case 'SET_BACKEND':
      return { ...state, backendOnline: action.online }
    default:
      return state
  }
}

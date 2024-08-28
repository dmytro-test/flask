export const NODE_TYPES = {
  ROOT: 'ROOT',
  CLASS: 'CLASS',
  METHOD: 'METHOD',
  ACTION: 'ACTION'
};

export const NODE_RISK_TYPES = {
  NORMAL: 'NORMAL',
  CHECK: 'CHECK',
  SUSPICIOUS: 'SUSPICIOUS'
};

export const LINK_TYPES = {
  NORMAL: 'NORMAL',
  CHECK: 'CHECK',
  SUSPICIOUS: 'SUSPICIOUS'
};

export const CONFIG = {
  SIMULATION: {
    TICK_DURATION: 300,
    LINK_STRENGTH: {
      ROOT: 0.3,
      CLASS: 0.1,
      SERVICE: 0.01,
      DEFAULT: 0.1
    },
    LINK_DISTANCE: {
      DEFAULT: 300,
      CLASS: 300,
      ROOT: 300
    },
    CHARGE_FORCE: {
      MIN_STRENGTH: -300,
      MAX_STRENGTH: -200
    },
    NODES_THRESHOLD: 10,
    ALPHA: 0.7,
    ALPHA_DECAY: 0.05,
    ALPHA_TARGET: 0.0001,
    DRAG_ALPHA_TARGET: 0.002
  },
  ZOOM: {
    MAX: 20
  },
  NODE: {
    G_CLASS_NAME: 'container',
    CLASS_NAME: 'graph-node',
    MIN_NODES_LENGTH: 10,
    STROKE: {
      [NODE_TYPES.ROOT]: 'grey',
      [NODE_TYPES.CLASS]: 'black',
      [NODE_TYPES.METHOD]: 'black',
      [NODE_TYPES.ACTION]: 'black'
    },
    RISK_TYPE: {
      [NODE_RISK_TYPES.NORMAL]: 'black',
      [NODE_RISK_TYPES.CHECK]: 'orange',
      [NODE_RISK_TYPES.SUSPICIOUS]: 'red'
    },

    ROOT: {
      RADIUS: 42,
      STROKE_WIDTH: 4,
      TEXT_DY: 5
    },
    DEFAULT: {
      RADIUS: 35,
      STROKE_WIDTH: 2,
      TEXT_DY: 5,
      TEXT_ANCHOR: 'middle',
      FONT_SIZE: 14,
      MAX_TEXT_LENGTH: 10
    }
  },
  LINK: {
    CLASS_NAME: 'graph-link',
    OPACITY: 1,
    FILL: 'none',
    STROKE: {
      [LINK_TYPES.NORMAL]: 'black',
      [LINK_TYPES.CHECK]: 'orange',
      [LINK_TYPES.SUSPICIOUS]: 'red'
    },
    STROKE_WIDTH: {
      [LINK_TYPES.NORMAL]: 1,
      [LINK_TYPES.CHECK]: 2,
      [LINK_TYPES.SUSPICIOUS]: 2
    },
    DASH: '4,4'
  }
};

export const SELECTORS = {
  TEXT: 'text'
};

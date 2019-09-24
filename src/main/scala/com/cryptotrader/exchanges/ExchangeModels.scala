package com.cryptotrader.exchanges

object ExchangeModels {

  trait Market {
    def name: String
    def baseCurrency: String
    def quoteCurrency: String
    def minPrice: BigDecimal
    def minVolume: BigDecimal
    def tickPrice: BigDecimal
    def tickVolume: BigDecimal
    def active: Boolean
  }
  /*
    "symbols": [
      {
        "symbol": "ETHBTC",
        "baseAsset": "ETH",
        "quoteAsset": "BTC",
        "status": "TRADING",
        "baseAssetPrecision": 8,
        "quotePrecision": 8,
      }
    ]

      "markets": [
      {
        "MarketName": "BTC-LTC",
        "BaseCurrency": "BTC",
        "MarketCurrency": "LTC",
        "IsActive": true,
        "MinTradeSize": 0.01,
      }
    ]

  "symbols": [
    {
      "id": "ETHBTC",
      "baseCurrency": "ETH",
      "quoteCurrency": "BTC",
      "quantityIncrement": "0.001",
      "tickSize": "0.000001",
    }
  ]


  */

  trait Ticker {
    def market: String
    def ask: BigDecimal
    def bid: BigDecimal
    def open: BigDecimal
    def high: BigDecimal
    def low: BigDecimal
    def last: BigDecimal
    def baseVolume: BigDecimal
    def quoteVolume: BigDecimal
    def timestamp: Long
  }

  /*
  "tickers": [
  {
    "symbol": "BNBBTC",
    "lastPrice": "4.00000200",
    "bidPrice": "4.00000000",
    "askPrice": "4.00000200",
    "openPrice": "99.00000000",
    "highPrice": "100.00000000",
    "lowPrice": "0.10000000",
    "volume": "8913.30000000",
    "quoteVolume": "15.30000000",
    "closeTime": 1499869899040,
  }
]
  "marketsummaries": [
    {
      "MarketName": "BTC-LTC",
      "High": 0.0135,
      "Low": 0.012,
      "Volume": 3833.97619253,
      "Last": 0.01349998,
      "BaseVolume": 47.03987026,
      "TimeStamp": "2014-07-09T07:22:16.72",
      "Bid": 0.01271001,
      "Ask": 0.012911,
      "PrevDay": 0.01229501,
    }
  ]

  "tickers": [
  {
    "ask": "0.050043",
    "bid": "0.050042",
    "last": "0.050042",
    "open": "0.047800",
    "low": "0.047052",
    "high": "0.051679",
    "volume": "36456.720",
    "volumeQuote": "1782.625000",
    "timestamp": "2017-05-12T14:57:19.999Z",
    "symbol": "ETHBTC"
  }
]
   */

  trait OrderBook{
    def buy: List[OrderPage]
    def sell: List[OrderPage]
  }

  trait OrderPage {
    def price: BigDecimal
    def volume: BigDecimal
  }

  /*
  {
  "bids": [
    [
      "4.00000000",     // PRICE
      "431.00000000"    // QTY
    ]
  ],
  "asks": [
    [
      "4.00000200",
      "12.00000000"
    ]
  ]
}

  "result": [
    {
      "buy": [
        {
          "quantity": 12.37,
          "rate": 32.55412402
        }
      ],
      "sell": [
        {
          "quantity": 12.37,
          "rate": 32.55412402
        }
      ]
    }
  ]
{
  "ask": [
    {
      "price": "0.046002",
      "size": "0.088"
    },
    {
      "price": "0.046800",
      "size": "0.200"
    }
  ],
  "bid": [
    {
      "price": "0.046001",
      "size": "0.005"
    },
    {
      "price": "0.046000",
      "size": "0.200"
    }
  ],
  "timestamp": "2018-11-19T05:00:28.193Z"
}
   */

  trait OrderSide
  object Buy extends OrderSide
  object Sell extends OrderSide

  trait Trade {
    def id: Long
    def price: BigDecimal
    def volume: BigDecimal
    def timestamp: Long
    def side: OrderSide
  }
  /*
  [
  {
    "id": 28457,
    "price": "4.00000100",
    "qty": "12.00000000",
    "quoteQty": "48.000012",
    "time": 1499865549590,
    "isBuyerMaker": true,
    "isBestMatch": true
  }
]
  "result": [
    {
      "Id": 319435,
      "TimeStamp": "2014-07-09T03:21:20.08",
      "Quantity": 0.30802438,
      "Price": 0.012634,
      "Total": 0.00389158,
      "FillType": "FILL",
      "OrderType": "BUY"
    }
  ]
  [
  {
    "id": 9533117,
    "price": "0.046001",
    "quantity": "0.220",
    "side": "sell",
    "timestamp": "2017-04-14T12:18:40.426Z"
  },
  {
    "id": 9533116,
    "price": "0.046002",
    "quantity": "0.022",
    "side": "buy",
    "timestamp": "2017-04-14T11:56:37.027Z"
  }
]
   */


  trait Balance {
    def currency: String
    def available: BigDecimal
    def reserved: BigDecimal
  }

  /*
    "balances": [
    {
      "asset": "BTC",
      "free": "4723846.89208129",
      "locked": "0.00000000"
    },
    {
      "asset": "LTC",
      "free": "4763368.68006011",
      "locked": "0.00000000"
    }
  ]
  "result": [
    {
      "Currency": "DOGE",
      "Available": 4.21549076,
      "Pending": 0,
    }
  ]
    [
      {
        "currency": "ETH",
        "available": "10.000000000",
        "reserved": "0.560000000"
      },
      {
        "currency": "BTC",
        "available": "0.010205869",
        "reserved": "0"
      }
    ]
   */

  trait Order {

  }
}

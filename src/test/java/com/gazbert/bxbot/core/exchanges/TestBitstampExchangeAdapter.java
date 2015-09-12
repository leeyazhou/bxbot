/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Gareth Jon Lynch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.gazbert.bxbot.core.exchanges;

import com.gazbert.bxbot.core.api.trading.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Tests the behaviour of the Bitstamp Exchange Adapter.
 * </p>
 *
 * <p>
 * Coverage could be better: it does not include calling the {@link BitstampExchangeAdapter#sendPublicRequestToExchange(String, Map)}
 * and {@link BitstampExchangeAdapter#sendAuthenticatedRequestToExchange(String, Map)} methods; the code in these methods
 * is a bloody nightmare to test!
 * </p>
 *
 * TODO Unit test {@link BitstampExchangeAdapter#sendPublicRequestToExchange(String, Map)} method.
 * TODO Unit test {@link BitstampExchangeAdapter#sendAuthenticatedRequestToExchange(String, Map)} method.
 *
 * @author gazbert
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.crypto.*")
@PrepareForTest(BitstampExchangeAdapter.class)
public class TestBitstampExchangeAdapter {

    // Valid config location - expected on runtime classpath in the ./src/test/resources folder.
    private static final String VALID_CONFIG_LOCATION = "bitstamp/bitstamp-config.properties";

    // Canned JSON responses from exchange - expected to reside on filesystem relative to project root
    private static final String ORDER_BOOK_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/order_book.json";
    private static final String OPEN_ORDERS_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/open_orders.json";
    private static final String BALANCE_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/balance.json";
    private static final String TICKER_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/ticker.json";
    private static final String BUY_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/buy.json";
    private static final String SELL_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/sell.json";
    private static final String CANCEL_ORDER_JSON_RESPONSE = "./src/test/exchange-data/bitstamp/cancel_order.json";

    // Exchange API calls
    private static final String ORDER_BOOK = "order_book";
    private static final String OPEN_ORDERS = "open_orders";
    private static final String BALANCE = "balance";
    private static final String TICKER = "ticker";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String CANCEL_ORDER = "cancel_order";

    // Canned test data
    private static final String MARKET_ID = "BTC_USD"; // can be anything for Bitstamp; not used as exchange only has 1 market.
    private static final BigDecimal BUY_ORDER_PRICE = new BigDecimal("200.18");
    private static final BigDecimal BUY_ORDER_QUANTITY = new BigDecimal("0.03");
    private static final BigDecimal SELL_ORDER_PRICE = new BigDecimal("250.176");
    private static final BigDecimal SELL_ORDER_QUANTITY = new BigDecimal("0.03");
    private static final String ORDER_ID_TO_CANCEL = "80894263";

    // Mocked out methods
    private static final String MOCKED_GET_CONFIG_LOCATION_METHOD = "getConfigFileLocation";
    private static final String MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD = "sendAuthenticatedRequestToExchange";
    private static final String MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD = "sendPublicRequestToExchange";

    /**
     * Bitstamp exchange Date format: 2015-01-09 21:14:50
     */
    private static final SimpleDateFormat EXCHANGE_DATE_FORMAT = new SimpleDateFormat("y-M-d H:m:s");


    // ------------------------------------------------------------------------------------------------
    //  Cancel Order tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testCancelOrderIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(CANCEL_ORDER_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final boolean success = exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);
        assertTrue(success);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testCancelOrderHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Traveling through hyperspace ain't like dusting crops, boy!" +
                        " Without precise calculations we could fly right through a star, or bounce too close to a " +
                        "supernova and that'd end your trip real quick, wouldn't it?"));

        PowerMock.replayAll();

        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCancelOrderHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER), anyObject(Map.class)).
                andThrow(new IllegalStateException("The Force is strong with this one."));

        PowerMock.replayAll();

        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Create Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testCreateOrderToBuyIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(BUY_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BUY), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.BUY, BUY_ORDER_QUANTITY, BUY_ORDER_PRICE);
        assertTrue(orderId.equals("80890994"));

        PowerMock.verifyAll();
    }

    @Test
    public void testCreateOrderToSellIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(SELL_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(SELL), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
        assertTrue(orderId.equals("80890993"));

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testCreateOrderHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(SELL), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Aaaaaaaaaaaaaaaarrrgh!"));

        PowerMock.replayAll();

        exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCreateOrderHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BUY), anyObject(Map.class)).
                andThrow(new IllegalArgumentException("That's 'cause droids don't pull people's arms out of their " +
                        "sockets when they lose. Wookiees are known to do that."));

        PowerMock.replayAll();

        exchangeAdapter.createOrder(MARKET_ID, OrderType.BUY, BUY_ORDER_QUANTITY, BUY_ORDER_PRICE);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Market Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingMarketOrdersSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDER_BOOK_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_BOOK), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final MarketOrderBook marketOrderBook = exchangeAdapter.getMarketOrders(MARKET_ID);

        // assert some key stuff; we're not testing GSON here.
        assertTrue(marketOrderBook.getMarketId().equals(MARKET_ID));

        final BigDecimal buyPrice = new BigDecimal("230.34");
        final BigDecimal buyQuantity = new BigDecimal("7.22860000");
        final BigDecimal buyTotal = buyPrice.multiply(buyQuantity);

        assertTrue(marketOrderBook.getBuyOrders().size() == 1268); // stamp send them all back!
        assertTrue(marketOrderBook.getBuyOrders().get(0).getType() == OrderType.BUY);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getPrice().compareTo(buyPrice) == 0);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getQuantity().compareTo(buyQuantity) == 0);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getTotal().compareTo(buyTotal) == 0);

        final BigDecimal sellPrice = new BigDecimal("230.90");
        final BigDecimal sellQuantity = new BigDecimal("0.62263188");
        final BigDecimal sellTotal = sellPrice.multiply(sellQuantity);

        assertTrue(marketOrderBook.getSellOrders().size() == 1957); // stamp send them all back!
        assertTrue(marketOrderBook.getSellOrders().get(0).getType() == OrderType.SELL);
        assertTrue(marketOrderBook.getSellOrders().get(0).getPrice().compareTo(sellPrice) == 0);
        assertTrue(marketOrderBook.getSellOrders().get(0).getQuantity().compareTo(sellQuantity) == 0);
        assertTrue(marketOrderBook.getSellOrders().get(0).getTotal().compareTo(sellTotal) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingMarketOrdersHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_BOOK), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Traveling through hyperspace ain’t like dusting crops, farm boy."));

        PowerMock.replayAll();

        exchangeAdapter.getMarketOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingMarketOrdersHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_BOOK), anyObject(Map.class)).
                andThrow(new IllegalArgumentException("Uh, we had a slight weapons malfunction, but uh... " +
                        "everything's perfectly all right now. We're fine. We're all fine here now, thank you. How are you?"));

        PowerMock.replayAll();

        exchangeAdapter.getMarketOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Your Open Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingYourOpenOrdersSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(OPEN_ORDERS_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(OPEN_ORDERS),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final List<OpenOrder> openOrders = exchangeAdapter.getYourOpenOrders(MARKET_ID);

        // assert some key stuff; we're not testing GSON here.
        assertTrue(openOrders.size() == 2);
        assertTrue(openOrders.get(0).getMarketId().equals(MARKET_ID));
        assertTrue(openOrders.get(0).getId().equals("52603560"));
        assertTrue(openOrders.get(0).getType() == OrderType.SELL);
        assertTrue(openOrders.get(0).getCreationDate().getTime() == EXCHANGE_DATE_FORMAT.parse("2015-01-09 21:14:50").getTime());
        assertTrue(openOrders.get(0).getPrice().compareTo(new BigDecimal("350.00")) == 0);
        assertTrue(openOrders.get(0).getTotal().compareTo(openOrders.get(0).getQuantity().multiply(openOrders.get(0).getPrice())) == 0);

        // the values below are not provided by Bitstamp
        assertNull(openOrders.get(0).getOriginalQuantity());

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingYourOpenOrdersHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(OPEN_ORDERS), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("The board is green!"));

        PowerMock.replayAll();

        exchangeAdapter.getYourOpenOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingYourOpenOrdersHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(OPEN_ORDERS), anyObject(Map.class)).
                andThrow(new IllegalStateException("You may dispense with the pleasantries, Commander. I am here to put you back on schedule."));

        PowerMock.replayAll();

        exchangeAdapter.getYourOpenOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Latest Market Price tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingLatestMarketPriceSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(TICKER_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(TICKER), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BigDecimal latestMarketPrice = exchangeAdapter.getLatestMarketPrice(MARKET_ID).setScale(8, BigDecimal.ROUND_HALF_UP);
        assertTrue(latestMarketPrice.compareTo(new BigDecimal("230.33")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingLatestMarketPriceHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(TICKER), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Jumping in 5... 4... 3... 2... 1... Jump!"));

        PowerMock.replayAll();

        exchangeAdapter.getLatestMarketPrice(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingLatestMarketPriceHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(TICKER), anyObject(Map.class)).
                andThrow(new IllegalArgumentException("Sir, the possibility of successfully navigating an asteroid field" +
                        " is approximately 3,720 to 1."));

        PowerMock.replayAll();

        exchangeAdapter.getLatestMarketPrice(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Balance Info tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingBalanceInfoSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(BALANCE_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BalanceInfo balanceInfo = exchangeAdapter.getBalanceInfo();

        // assert some key stuff; we're not testing GSON here.
        assertTrue(balanceInfo.getBalancesAvailable().get("BTC").compareTo(new BigDecimal("0.28994223")) == 0);
        assertTrue(balanceInfo.getBalancesAvailable().get("USD").compareTo(new BigDecimal("0")) == 0);

        assertTrue(balanceInfo.getBalancesOnHold().get("BTC").compareTo(new BigDecimal("0.21655294")) == 0);
        assertTrue(balanceInfo.getBalancesOnHold().get("USD").compareTo(new BigDecimal("0")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingBalanceInfoHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("I’ve been waiting for you, Obi-Wan. We meet again, at last. " +
                        "The circle is now complete. When I left you, I was but the learner; now I am the master."));

        PowerMock.replayAll();

        exchangeAdapter.getBalanceInfo();

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingBalanceInfoHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE), anyObject(Map.class)).
                andThrow(new IllegalStateException("Get me some more frakking birds in the air!"));

        PowerMock.replayAll();

        exchangeAdapter.getBalanceInfo();

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Exchange Fees for Buy orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingExchangeBuyingFeeSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(BALANCE_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BigDecimal buyPercentageFee = exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);
        assertTrue(buyPercentageFee.compareTo(new BigDecimal("0.0025")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingExchangeBuyingFeeHandlesTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Aren't you a little short for a stormtrooper?"));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingExchangeBuyingFeeHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE), anyObject(Map.class)).
                andThrow(new IllegalStateException("I felt a great disturbance in the Force, as if millions of voices" +
                        " suddenly cried out in terror and were suddenly silenced. I fear something terrible has happened."));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Exchange Fees for Sell orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingExchangeSellingFeeSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(BALANCE_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitstampExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BigDecimal sellPercentageFee = exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);
        assertTrue(sellPercentageFee.compareTo(new BigDecimal("0.0025")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingExchangeSellingFeeHandlesTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("That's no moon. It's a space station."));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingExchangeSellingFeeHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitstampExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitstampExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCE), anyObject(Map.class)).
                andThrow(new IllegalStateException("Don't be too proud of this technological terror you've constructed." +
                        " The ability to destroy a planet is insignificant next to the power of the Force."));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Non Exchange visiting tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingImplNameIsAsExpected() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final BitstampExchangeAdapter exchangeAdapter = new BitstampExchangeAdapter();
        assertTrue(exchangeAdapter.getImplName().equals("Bitstamp HTTP API v1"));

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Initialisation tests - assume config property files are located under src/test/resources
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testExchangeAdapterInitialisesSuccessfully() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final BitstampExchangeAdapter exchangeAdapter = new BitstampExchangeAdapter();
        assertNotNull(exchangeAdapter);

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfPClientIdConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitstamp/missing-clientid-bitstamp-config.properties");
        PowerMock.replayAll();

        new BitstampExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfPublicKeyConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitstamp/missing-public-key-bitstamp-config.properties");
        PowerMock.replayAll();

        new BitstampExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfSecretConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitstamp/missing-secret-bitstamp-config.properties");
        PowerMock.replayAll();

        new BitstampExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfTimeoutConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitstamp/missing-timeout-bitstamp-config.properties");
        PowerMock.replayAll();

        new BitstampExchangeAdapter();

        PowerMock.verifyAll();
    }

    /*
     * Used for making real API calls to the exchange in order to grab JSON responses.
     * Have left this in; it might come in useful.
     * It expects VALID_CONFIG_LOCATION to contain the correct credentials.
     */
    //@Test
    public void testCallingExchangeToGetJson() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitstampExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        //final BitstampExchangeAdapter exchangeAdapter = new BitstampExchangeAdapter();
        //exchangeAdapter.getLatestMarketPrice(MARKET_ID);
        //exchangeAdapter.getBalanceInfo();

        PowerMock.verifyAll();
    }
}

package ru.algeps.sparrow.channel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.concurrent.ExecutorService;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SslByteChannelTest {
  private ByteChannel mockByteChannel;
  private SSLEngine mockSslEngine;
  private SSLSession mockSslSession;
  private ExecutorService mockExecutorService;
  private SslByteChannel sslByteChannel;

  @BeforeEach
  void setUp() {
    mockByteChannel = Mockito.mock(ByteChannel.class);
    mockSslEngine = Mockito.mock(SSLEngine.class);
    mockSslSession = Mockito.mock(SSLSession.class);
    mockExecutorService = Mockito.mock(ExecutorService.class);
    when(mockSslEngine.getSession()).thenReturn(mockSslSession);
    when(mockSslSession.getPacketBufferSize()).thenReturn(16384);
    when(mockSslSession.getApplicationBufferSize()).thenReturn(16384);
    sslByteChannel = new SslByteChannel(mockByteChannel, mockSslEngine);
  }

  @Test
  void test_constructor_nullByteChannel() {
    assertThrows(NullPointerException.class, () -> new SslByteChannel(null, mockSslEngine));
  }

  @Test
  void test_constructor_nullSslEngine() {
    assertThrows(NullPointerException.class, () -> new SslByteChannel(mockByteChannel, null));
  }

  @Test
  void test_read_emptyPeerAppData_channelClosed() throws IOException {
    when(mockByteChannel.read(any(ByteBuffer.class))).thenReturn(-1);
    assertThrows(
        SslByteChannel.SslByteChannelException.class,
        () -> sslByteChannel.read(ByteBuffer.allocate(10)));
  }

  @Test
  void test_write_channelClosed() throws IOException {
    when(mockByteChannel.write(any(ByteBuffer.class))).thenReturn(-1);
    when(mockSslEngine.wrap(any(ByteBuffer.class), any(ByteBuffer.class)))
        .thenReturn(
            new SSLEngineResult(
                SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0));
    ByteBuffer src = ByteBuffer.wrap(new byte[] {1, 2, 3});
    assertThrows(SslByteChannel.SslByteChannelException.class, () -> sslByteChannel.write(src));
  }

  @Test
  void test_doHandshake_needUnwrap_bufferUnderflow() throws IOException {
    when(mockSslEngine.getHandshakeStatus())
        .thenReturn(
            SSLEngineResult.HandshakeStatus.NEED_UNWRAP, SSLEngineResult.HandshakeStatus.FINISHED);
    when(mockByteChannel.read(any(ByteBuffer.class))).thenReturn(3);
    when(mockSslEngine.unwrap(any(ByteBuffer.class), any(ByteBuffer.class)))
        .thenReturn(
            new SSLEngineResult(
                SSLEngineResult.Status.BUFFER_UNDERFLOW,
                SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                0,
                0));
    sslByteChannel.startHandshake();
  }

  @Test
  void test_doHandshake_needUnwrap_ok() throws IOException {
    when(mockSslEngine.getHandshakeStatus())
        .thenReturn(
            SSLEngineResult.HandshakeStatus.NEED_UNWRAP, SSLEngineResult.HandshakeStatus.FINISHED);
    when(mockByteChannel.read(any(ByteBuffer.class))).thenReturn(3);
    when(mockSslEngine.unwrap(any(ByteBuffer.class), any(ByteBuffer.class)))
        .thenReturn(
            new SSLEngineResult(
                SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 3, 3));
    sslByteChannel.startHandshake();
  }
}

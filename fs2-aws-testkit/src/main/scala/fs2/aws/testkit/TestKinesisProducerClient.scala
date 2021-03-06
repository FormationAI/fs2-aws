package fs2.aws.testkit

import java.nio.ByteBuffer

import cats.effect.Effect
import com.amazonaws.services.kinesis.producer.UserRecordResult
import com.google.common.util.concurrent.ListenableFuture
import fs2.aws.internal.KinesisProducerClient

case class TestKinesisProducerClient[F[_]](respondWith: UserRecordResult,
                                           ops: F[ListenableFuture[UserRecordResult]])
    extends KinesisProducerClient[F] {
  override def putData(streamName: String, partitionKey: String, data: ByteBuffer)(
      implicit e: Effect[F]): F[ListenableFuture[UserRecordResult]] = {
    ops
  }
}

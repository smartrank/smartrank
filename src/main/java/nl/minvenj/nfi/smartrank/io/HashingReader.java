/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.io;

import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingReader extends Reader {

    private static final String[] HEXDIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private final Reader _parent;
    private MessageDigest _hasher;
    private String _hash = null;
    private long _offset;

    public HashingReader(final Reader parent) {
        _parent = parent;
        try {
            _hasher = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException ex) {
            _hasher = new XORDigest();
        }
        _offset = 0;
    }

    public String getHash() {
        if (_hash == null) {
            final StringBuilder builder = new StringBuilder(_hasher.getAlgorithm()).append("/");
            final byte[] digestBytes = _hasher.digest();
            for (int idx = 0; idx < digestBytes.length; idx++) {
                builder.append(HEXDIGITS[(digestBytes[idx] >> 4) & 0x0F]).append(HEXDIGITS[digestBytes[idx] & 0x0F]);
            }
            _hash = builder.toString();
        }

        return _hash;
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        final char[] temp = new char[len];
        final int charsRead = _parent.read(temp, 0, len);
        int reportedLength = charsRead;
        if (charsRead >= 0) {
            final byte[] bytes = new byte[charsRead];
            for (int idx = 0; idx < charsRead; idx++) {
                bytes[idx] = (byte) temp[idx];
            }
            _hasher.update(bytes, 0, charsRead);

            int copyFromIdx = 0;
            if (_offset == 0) {
                // Skip any non-printable characters at the start of the string and copy the rest to the output
                while (copyFromIdx < charsRead && temp[copyFromIdx] >= 0x007F) {
                    copyFromIdx++;
                }
            }

            // Adjust the number of characters that we will report to the caller to compensate for any non-character bytes we skipped
            reportedLength -= copyFromIdx;

            // Copy the characters we read to the output array, skipping any non-character data prefix
            System.arraycopy(temp, copyFromIdx, cbuf, off, len - copyFromIdx);
            _offset += charsRead;
            _hash = null;
        }
        return reportedLength;
    }

    @Override
    public void close() throws IOException {
        _parent.close();
    }

    public long getOffset() {
        return _offset;
    }

    private static class XORDigest extends MessageDigest {

        byte _digest;

        public XORDigest() {
            super("XOR");
            _digest = 0;
        }

        @Override
        protected void engineUpdate(final byte input) {
            _digest ^= input;
        }

        @Override
        protected void engineUpdate(final byte[] input, final int offset, final int len) {
            for (int idx = offset; idx < (offset + len); idx++) {
                update(input[idx]);
            }
        }

        @Override
        protected byte[] engineDigest() {
            return new byte[]{_digest};
        }

        @Override
        protected void engineReset() {
            _digest = (byte) 0xA5;
        }
    }
}

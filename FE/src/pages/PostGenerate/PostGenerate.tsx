import React, { useState } from 'react';
import { Input, Form, Upload, Select, Image, Button } from 'antd';
import { CloseOutlined, FileImageOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';
import { v4 as uuidv4 } from 'uuid';

import './PostGenerate.styles.scss';
import FixedBottomButton from '@components/FixedBottomButton/FixedBottomButton';
import SearchHeader from '@components/SearchHeader/SearchHeader';
import { useNavigate, useParams } from 'react-router-dom';
import { useUser } from '@src/hooks/useUser';
import { clubCreatePostAPI } from '@src/API/clubAPI';
import axios from 'axios';

let images: any = [];
let globalLoading = false;
const props: UploadProps = {
  name: 'file',
  multiple: true,

  customRequest: async (data: any) => {
    globalLoading = true;
    let formData = new FormData();
    formData.append('file', data.file);
    formData.append('upload_preset', 'quzqjwbp');
    const { data: response } = await axios.post(
      'https://api.cloudinary.com/v1_1/dohkkln9r/image/upload',
      formData,
    );
    images.push({ uid: data.file.uid, url: response.url });
    globalLoading = false;
    data.onSuccess('ok');
  },
  itemRender: (_, file: any, __, { remove }) => {
    const url = URL.createObjectURL(file.originFileObj);

    return (
      <div style={{ position: 'relative', display: 'inline-block' }}>
        <Image
          width={'100%'}
          style={{
            objectFit: 'cover',
            aspectRatio: '1',
            borderRadius: '1rem',
          }}
          src={url}
          alt=""
        />
        <Button
          style={{
            position: 'absolute',
            top: '-8px',
            right: '-8px',
          }}
          onClick={() => {
            images = images.filter((image: any) => image.uid != file.uid);
            remove();
          }}
          danger
          shape="circle"
          icon={<CloseOutlined />}
        />
      </div>
    );
  },
};

function Label({ text }: { text: string }) {
  return <h3 style={{ fontSize: '1.2rem', fontFamily: 'NEXON' }}>{text}</h3>;
}

function PostGenerate() {
  const [form] = Form.useForm();
  const { id: groupId } = useParams();
  const user = useUser();
  const navigate = useNavigate();
  const onFinish = async (values: any) => {
    if (!groupId || !user) return;

    await clubCreatePostAPI({
      ...values,
      groupId: +groupId,
      writer: user?.memberId,
      postImage: images.map((image: any) => image.url),
    });
    navigate(`/book-club/${groupId}`);
  };

  return (
    <>
      <SearchHeader text="????????? ????????????" search={false} />
      <div className="post-generate-container">
        <Form form={form} onFinish={onFinish}>
          <Form.Item
            label={<Label text="??????" />}
            name="postTitle"
            rules={[{ required: true, message: '????????? ???????????????' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label={<Label text="??????" />}
            name="postContent"
            rules={[{ required: true, message: '????????? ???????????????' }]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item
            label={<Label text="????????? ??????" />}
            rules={[{ required: true, message: '???????????? ??????????????????' }]}
            initialValue={'FREE'}
            name="postCategory"
          >
            <Select style={{ fontFamily: 'NEXON' }}>
              <Select.Option value="FREE">?????????</Select.Option>
              <Select.Option value="NOTICE">????????????</Select.Option>
              <Select.Option value="RECOMMEND">??? ??????</Select.Option>
              <Select.Option value="GREET">????????????</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            label={<Label text="??????" />}
            name="postImage"
            valuePropName="any"
          >
            <Upload.Dragger {...props}>
              <div className="ant-upload-container">
                <p>????????????</p>
                <FileImageOutlined className="image-icon" />
              </div>
            </Upload.Dragger>
          </Form.Item>
        </Form>
      </div>
      <FixedBottomButton
        text="????????????"
        onClick={() => {
          console.log(form.submit());
        }}
      />
    </>
  );
}

export default PostGenerate;
